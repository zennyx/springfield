package zenny.toybox.springfield.data.mybatis.generator;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ParamsProcessor extends AbstractProcessor {

  private JavacTrees trees;
  private TreeMaker treeMaker;
  private Names names;

  /*
   * (non-Javadoc)
   * @see javax.annotation.processing.AbstractProcessor#init(javax.annotation.
   * processing.ProcessingEnvironment)
   */
  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
    this.trees = JavacTrees.instance(processingEnv);
    this.treeMaker = TreeMaker.instance(context);
    this.names = Names.instance(context);
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    roundEnv.getElementsAnnotatedWith(Params.class).stream()
        .map(element -> this.trees.getTree(element))
        .forEach(tree -> tree.accept(new TreeTranslator() {
          @Override
          public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
            ParamsProcessor.this.prependParamAnnotation(jcClassDecl);
            super.visitClassDef(jcClassDecl);
          }
        }));

    return true;
  }

  private void prependParamAnnotation(JCTree.JCClassDecl jcClassDecl) {
    jcClassDecl.defs.stream()
        .filter(element -> element.getKind().equals(Tree.Kind.METHOD))
        .map(methodTree -> (JCTree.JCMethodDecl) methodTree).forEach(methodTree -> {
          methodTree.getParameters().forEach(parameter -> {
            JCTree.JCAnnotation paramAnnotation = this.createParamAnnotation(parameter);
            parameter.getModifiers().annotations.append(paramAnnotation);
          });
        });
  }

  private JCTree.JCAnnotation createParamAnnotation(JCTree.JCVariableDecl parameter) {
    return this.treeMaker.Annotation(this.treeMaker.Ident(this.names.fromString("Param")),
        List.of(this.treeMaker.Assign(this.treeMaker.Ident(this.names.fromString("value")),
            this.treeMaker.Literal(parameter.name.toString()))));
  }
}

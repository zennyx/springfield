package zenny.toybox.springfield.data.mybatis.repository.config;

import org.springframework.instrument.classloading.ShadowingClassLoader;

/**
 * Disposable {@link ClassLoader} used to inspect user-code classes within an
 * isolated class loader without preventing class transformation at a later
 * time.
 *
 * @author Zenny Xu
 */
class InspectionClassLoader extends ShadowingClassLoader {

  /**
   * Create a new {@link InspectionClassLoader} instance.
   *
   * @param parent the parent classloader
   */
  public InspectionClassLoader(ClassLoader parent) {
    super(parent, true);

    this.excludePackage("org.springframework.");
    this.excludePackage("zenny.toybox.springfield.");
  }
}

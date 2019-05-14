package zenny.toybox.springfield.data.mybatis.config;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class PageProxy<T> implements Page<T> {

  private Page<T> page = Page.empty();

  @Override
  public int getNumber() {
    return this.page.getNumber();
  }

  @Override
  public int getSize() {
    return this.page.getSize();
  }

  @Override
  public int getNumberOfElements() {
    return this.page.getNumberOfElements();
  }

  @Override
  public List<T> getContent() {
    return this.page.getContent();
  }

  @Override
  public boolean hasContent() {
    return this.page.hasContent();
  }

  @Override
  public Sort getSort() {
    return this.page.getSort();
  }

  @Override
  public boolean isFirst() {
    return this.page.isFirst();
  }

  @Override
  public boolean isLast() {
    return this.page.isLast();
  }

  @Override
  public boolean hasNext() {
    return this.page.hasNext();
  }

  @Override
  public boolean hasPrevious() {
    return this.page.hasPrevious();
  }

  @Override
  public Pageable nextPageable() {
    return this.page.nextPageable();
  }

  @Override
  public Pageable previousPageable() {
    return this.page.previousPageable();
  }

  @Override
  public Iterator<T> iterator() {
    return this.page.iterator();
  }

  @Override
  public int getTotalPages() {
    return this.page.getTotalPages();
  }

  @Override
  public long getTotalElements() {
    return this.page.getTotalElements();
  }

  @Override
  public <U> Page<U> map(Function<? super T, ? extends U> converter) {
    return this.page.map(converter);
  }

  @SuppressWarnings("unchecked")
  public void fill(List<? extends T> content) {
    if (PagingList.class.isInstance(content)) {
      PagingList<T> pagingContent = (PagingList<T>) content;
      this.page = new PageImpl<>(pagingContent, pagingContent.getPageable(), pagingContent.size());
    } else {
      this.page = new PageImpl<>((List<T>) content);
    }
  }
}

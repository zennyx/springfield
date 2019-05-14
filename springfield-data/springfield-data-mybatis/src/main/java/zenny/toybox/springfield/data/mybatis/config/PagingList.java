package zenny.toybox.springfield.data.mybatis.config;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.springframework.data.domain.Pageable;

class PagingList<E> implements List<E> {

  private final List<E> content;
  private final Pageable pageable;

  public PagingList(List<E> content, Pageable pageable) {
    this.content = content;
    this.pageable = pageable;
  }

  @Override
  public Iterator<E> iterator() {
    return this.content.iterator();
  }

  @Override
  public int size() {
    return this.content.size();
  }

  @Override
  public boolean isEmpty() {
    return this.content.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return this.content.contains(o);
  }

  @Override
  public Object[] toArray() {
    return this.content.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return this.content.toArray(a);
  }

  @Override
  public boolean add(E e) {
    return this.content.add(e);
  }

  @Override
  public boolean remove(Object o) {
    return this.content.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return this.content.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return this.content.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return this.content.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return this.content.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return this.content.retainAll(c);
  }

  @Override
  public void clear() {
    this.content.clear();
  }

  @Override
  public E get(int index) {
    return this.content.get(index);
  }

  @Override
  public E set(int index, E element) {
    return this.content.set(index, element);
  }

  @Override
  public void add(int index, E element) {
    this.content.add(index, element);
    ;
  }

  @Override
  public E remove(int index) {
    return this.content.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return this.content.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return this.content.lastIndexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return this.content.listIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    return this.content.listIterator(index);
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    return this.content.subList(fromIndex, toIndex);
  }

  public Pageable getPageable() {
    return this.pageable;
  }
}

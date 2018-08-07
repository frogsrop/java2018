package ru.ifmo.rain.ugay.arrayset;

import java.util.*;

import static java.util.Collections.unmodifiableList;


public class ArraySet<E> extends AbstractSet<E> implements NavigableSet<E> {

    class DecList<T> extends AbstractList<E> implements List<E> {

        private List<E> arr;
        private boolean inv;

        DecList(List<E> x) {
            if(x instanceof DecList)
            {
                DecList<E> y = (DecList<E>)x;
                inv = !y.inv;
                arr = y.arr;
            }
            else
            {
                inv = true;
                arr = x;
            }
        }

        @Override
        public E get(int index) {
            if (inv)
                return arr.get(arr.size() - 1 - index);
            else
                return arr.get(index);
        }

        @Override
        public int size() {
            return arr.size();
        }
    }

    private List<E> arr;

    private Comparator<E> comp;

    public ArraySet() {
        this(new ArrayList<>(), null);
    }

    private ArraySet(Comparator<E> comp1) {
        this(new ArrayList<>(), comp1);
    }

    public ArraySet(Collection<E> x) {
        this(x, null);
    }

    public ArraySet(Collection<E> x, Comparator<E> comp1) {
        comp = comp1;
        TreeSet<E> ts = new TreeSet<>(comp);
        ts.addAll(x);
        arr = new ArrayList<>(ts);
    }

    @Override
    public boolean contains(Object o) {
        return Collections.binarySearch(arr, (E) o, comp) >= 0;
    }

    private E result(int x) {
        if (x <= -1 || x >= arr.size())
            return null;
        else
            return arr.get(x);
    }

    @Override
    public E lower(E e) {
        int pos = Collections.binarySearch(arr, e, comp);
        if (pos >= 0)
            return result(pos - 1);
        else {
            return result(-(pos + 1) - 1);
        }
    }

    @Override
    public E floor(E e) {
        int pos = Collections.binarySearch(arr, e, comp);
        if (pos >= 0) {
            return result(pos);
        } else {
            return result(-(pos + 1) - 1);
        }
    }

    @Override
    public E ceiling(E e) {
        int pos = Collections.binarySearch(arr, e, comp);
        if (pos >= 0)
            return result(pos);
        else
            return result(-(pos + 1));
    }

    @Override
    public E higher(E e) {
        int pos = Collections.binarySearch(arr, e, comp);
        if (pos >= 0)
            return result(pos + 1);
        else
            return result(-(pos + 1));
    }

    @Override
    public E pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public E pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return unmodifiableList(arr).iterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        ArraySet<E> temp = new ArraySet<>();
        temp.arr = new DecList<E>(arr);
        temp.comp = comp.reversed();
        return temp;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        int from = getBounds(fromElement, !fromInclusive);
        int to = getBounds(toElement, toInclusive);
        ArraySet<E> ret = new ArraySet<>();
        ret.comp = comp;

        if (to > from && from >= 0 && to <= arr.size()) {
            ret.arr = arr.subList(from, to);
        }
        return ret;
    }

    private int getBounds(E fromElement, boolean b) {
        int from = Collections.binarySearch(arr, fromElement, comp);
        if (from >= 0 && b) {
            from = from + 1;
        } else if (from < 0) {
            from = -(from + 1);
        }
        return from;
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(arr.get(0), true, toElement, inclusive);
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        if (isEmpty()) {
            return this;
        }
        return subSet(fromElement, inclusive, arr.get(arr.size() - 1), true);
    }

    @Override
    public Comparator<? super E> comparator() {
        return comp;
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        if (isEmpty()) {
            return this;
        }
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        if (isEmpty()) {
            return this;
        }
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        if (isEmpty()) {
            return this;
        }
        return tailSet(fromElement, true);
    }

    @Override
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return result(0);
    }

    @Override
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return result(arr.size() - 1);
    }

    @Override
    public int size() {
        return arr.size();
    }
}

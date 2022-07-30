package com.supermartijn642.core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created 30/07/2022 by SuperMartijn642
 */
public class MappedSetView<R, T> implements Set<T> {

    public static <R, T> Set<T> map(Set<R> set, Function<R,T> mapper){
        return new MappedSetView<>(set, mapper);
    }

    private final Set<R> set;
    private final Function<R,T> mapper;

    private MappedSetView(Set<R> set, Function<R,T> mapper){
        this.set = set;
        this.mapper = mapper;
    }

    @Override
    public int size(){
        return this.set.size();
    }

    @Override
    public boolean isEmpty(){
        return this.set.isEmpty();
    }

    @Override
    public boolean contains(Object o){
        throw new UnsupportedOperationException("Mapped sets cannot be checked for contained elements!");
    }

    @Override
    public Iterator<T> iterator(){
        Iterator<R> iterator = this.set.iterator();
        return new Iterator<T>() {
            @Override
            public boolean hasNext(){
                return iterator.hasNext();
            }

            @Override
            public T next(){
                return MappedSetView.this.mapper.apply(iterator.next());
            }
        };
    }

    @Override
    public Object[] toArray(){
        return this.set.stream().map(this.mapper).toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a){
        return this.set.stream().map(this.mapper).collect(Collectors.toList()).toArray(a);
    }

    @Override
    public boolean add(T t){
        throw new UnsupportedOperationException("Mapped set views cannot be added to!");
    }

    @Override
    public boolean remove(Object o){
        throw new UnsupportedOperationException("Mapped set views cannot be removed from!");
    }

    @Override
    public boolean containsAll(Collection<?> c){
        throw new UnsupportedOperationException("Mapped sets cannot be checked for contained elements!");
    }

    @Override
    public boolean addAll(Collection<? extends T> c){
        throw new UnsupportedOperationException("Mapped set views cannot be added to!");
    }

    @Override
    public boolean retainAll(Collection<?> c){
        throw new UnsupportedOperationException("Mapped set views cannot be removed from!");
    }

    @Override
    public boolean removeAll(Collection<?> c){
        throw new UnsupportedOperationException("Mapped set views cannot be removed from!");
    }

    @Override
    public void clear(){
        throw new UnsupportedOperationException("Mapped set views cannot be removed from!");
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter){
        throw new UnsupportedOperationException("Mapped set views cannot be removed from!");
    }
}

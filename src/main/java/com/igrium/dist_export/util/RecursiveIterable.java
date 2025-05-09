package com.igrium.dist_export.util;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;


public class RecursiveIterable<T> implements Iterable<T> {
    private final Iterable<T>[] array;

    public RecursiveIterable(Iterable<T>[] array) {
        this.array = array;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return new RecursiveIterator();
    }

    private class RecursiveIterator implements Iterator<T> {

        private int nextIndex = 0;
        private Iterator<T> currentIter;

        @Override
        public boolean hasNext() {
            // Check if the current iterator still has elements
            if (currentIter != null && currentIter.hasNext()) {
                return true;
            }

            // Check subsequent iterables in the array
            while (nextIndex < array.length) {
                currentIter = array[nextIndex].iterator();
                nextIndex++;
                if (currentIter.hasNext()) {
                    return true;
                }
            }

            // No more elements left
            return false;
        }

        @Override
        public T next() {
            if (currentIter != null && currentIter.hasNext()) {
                return currentIter.next();
            } else if (nextIndex < array.length) {
                currentIter = array[nextIndex].iterator();
                nextIndex++;
                return next();
            } else {
                throw new IndexOutOfBoundsException();
            }
        }
    }
}

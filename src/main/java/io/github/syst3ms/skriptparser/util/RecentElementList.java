package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A bare-bones linked list (despite it doesn't even extend {@link Collection}) putting forth the most recent elements through
 * {@link #moveToFirst(Object)}. This has been created to avoid {@linkplain ConcurrentModificationException}s in the
 * situation where an {@link io.github.syst3ms.skriptparser.registration.ExpressionInfo} should be moved to the front,
 * while parsing another expression.
 * @param <T> the type of the elements
 */
public class RecentElementList<T> implements Iterable<T> {
    @Nullable
    private Node<T> head;

    public RecentElementList() {}

    public void moveToFirst(T element) {
        Set<T> traversed = new HashSet<>();
        Node<T> node = head;
        do {
            if(head == null)
                break;
            Node<T> next = node.getNext();
            if (next != null) {
                T nextValue = next.getValue();
                if (element.equals(nextValue)) {
                    node.setNext(next.getNext()); // How confusing
                    if (traversed.contains(nextValue)) {
                        return;
                    } else {
                        traversed.add(nextValue);
                    }
                    head = new Node<>(nextValue, head);
                    return;
                }
            }
            node = node.getNext();
        } while (node != null);
        // the element wasn't present already
        if (head != null) {
            assert head.getValue() != null;
            if (head.getValue().equals(element)) {
                return;
            }
        }
        head = new Node<>(element, head);
    }

    public void removeFrom(Collection<? extends T> removeFrom) {
        Node<T> n = head;
        while (n != null) {
            removeFrom.remove(n.getValue());
            n = n.getNext();
        }
    }

    public int size() {
        int size = 0;
        Node<T> n = head;
        while (n != null) {
            n = n.next;
            size++;
        }
        return size;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Nullable
            Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Nullable
            @Override
            public T next() {
                if (current == null)
                    throw new NoSuchElementException();
                Node<T> c = current;
                current = c.getNext();
                return c.getValue();
            }
        };
    }

    private static class Node<T> {
        private T value;
        @Nullable
        private Node<T> next;

        Node(T value, @Nullable Node<T> next) {
            this.value = value;
            this.next = next;
        }

        T getValue() {
            return value;
        }

        void setValue(T value) {
            this.value = value;
        }

        @Nullable Node<T> getNext() {
            return next;
        }

        void setNext(@Nullable Node<T> next) {
            this.next = next;
        }
    }
}

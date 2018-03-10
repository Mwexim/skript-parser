package io.github.syst3ms.skriptparser.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            Node<T> current;

            @Override
            public boolean hasNext() {
                return current != null && current.getNext() != null;
            }

            @Nullable
            @Override
            public T next() {
                if (current == null ||current.getNext() == null)
                    throw new NoSuchElementException();
                Node<T> next = current.getNext();
                current = next;
                return next.getValue();
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

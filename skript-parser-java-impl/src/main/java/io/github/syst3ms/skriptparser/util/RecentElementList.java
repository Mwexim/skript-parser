package io.github.syst3ms.skriptparser.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class RecentElementList<T> implements Iterable<T> {
    private Node<T> head;

    public RecentElementList() {}

    public void moveToFirst(T element) {
        Set<T> traversed = new HashSet<>();
        Node<T> node = head;
        do {
            if(head == null)
                break;
            if (node.getNext() != null) {
                Node<T> next = node.getNext();
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
        if (head != null && head.getValue().equals(element))
            return;
        head = new Node<>(element, head);
    }

    public void removeFrom(Collection<? extends T> remove) {
        Node<T> n = head;
        while (n != null) {
            remove.remove(n.getValue());
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

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> current = new Node<>(null, head);

            @Override
            public boolean hasNext() {
                return current.getNext() != null;
            }

            @Override
            public T next() {
                if (current.getNext() == null)
                    throw new NoSuchElementException();
                Node<T> next = current.getNext();
                current = next;
                return next.getValue();
            }
        };
    }

    private static class Node<T> {
        private T value;
        private Node<T> next;

        Node(T value, Node<T> next) {
            this.setValue(value);
            this.setNext(next);
        }

        T getValue() {
            return value;
        }

        void setValue(T value) {
            this.value = value;
        }

        Node<T> getNext() {
            return next;
        }

        void setNext(Node<T> next) {
            this.next = next;
        }
    }
}

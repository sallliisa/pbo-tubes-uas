package workflow;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class ApprovalQueue<T> {
    private final Queue<T> queue = new LinkedList<>();

    public void submit(T item) {
        queue.offer(item);
    }

    public Optional<T> next() {
        return Optional.ofNullable(queue.poll());
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}

package ureka.framework.resource.communication.simulated_comm;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

import javax.swing.event.MenuDragMouseListener;

import ureka.framework.logic.DeviceController;

public class SimulatedCommChannel {
    private DeviceController end = null;
    private Queue<String> receiverQueue = null;
    private Queue<String> senderQueue = null;

    public SimulatedCommChannel() {}
    public SimulatedCommChannel(DeviceController end, Queue<String> receiverQueue, Queue<String> senderQueue) {
        this.end = end;
        this.receiverQueue = receiverQueue;
        this.senderQueue = senderQueue;
    }

    public DeviceController getEnd() {
        return end;
    }

    public void setEnd(DeviceController end) {
        this.end = end;
    }

    public Queue<String> getReceiverQueue() {
        return receiverQueue;
    }

    public void setReceiverQueue(Queue<String> receiverQueue) {
        this.receiverQueue = receiverQueue;
    }

    public Queue<String> getSenderQueue() {
        return senderQueue;
    }

    public void setSenderQueue(Queue<String> senderQueue) {
        this.senderQueue = senderQueue;
    }
}

// TODO:
// 1. Add the constructor, equal(), etc. if needed
// 2. Determine the data type in Queue
// 3. Check if circular import occurs

package ureka.framework.resource.communication.simulated_comm;

import java.util.concurrent.BlockingQueue;

import ureka.framework.logic.DeviceController;

public class SimulatedCommChannel {
    private DeviceController end = null;
    private BlockingQueue<String> receiverQueue = null;
    private BlockingQueue<String> senderQueue = null;

    public DeviceController getEnd() {
        return end;
    }

    public void setEnd(DeviceController end) {
        this.end = end;
    }

    public BlockingQueue<String> getReceiverQueue() {
        return receiverQueue;
    }

    public void setReceiverQueue(BlockingQueue<String> receiverQueue) {
        this.receiverQueue = receiverQueue;
    }

    public BlockingQueue<String> getSenderQueue() {
        return senderQueue;
    }

    public void setSenderQueue(BlockingQueue<String> senderQueue) {
        this.senderQueue = senderQueue;
    }
}

// TODO:
// 1. Add the constructor, equal(), etc. if needed
// 2. Determine the data type in Queue
// 3. Check if circular import occurs

package ureka.framework.logic.stage_worker;

import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleMeasurer;
import ureka.framework.resource.storage.SimpleStorage;

public class ReceivedMsgStorer {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private SimpleStorage simpleStorage;

    public ReceivedMsgStorer(SharedData sharedData, MeasureHelper measureHelper, SimpleStorage simpleStorage) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
        this.simpleStorage = simpleStorage;
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public SharedData getSharedData() {
        return sharedData;
    }

    public void setMeasureHelper(MeasureHelper measureHelper) {
        this.measureHelper = measureHelper;
    }

    public MeasureHelper getMeasureHelper() {
        return measureHelper;
    }

    public void setSimpleStorage(SimpleStorage simpleStorage) {
        this.simpleStorage = simpleStorage;
    }

    public SimpleStorage getSimpleStorage() {
        return simpleStorage;
    }

    // [STAGE: (SR)] Store Received Message
    public void storeReceivedXxxUTicket(UTicket receivedUTicket) {
        SimpleMeasurer.measureWorkerFunc(this::_storeReceivedXxxUTicket,receivedUTicket);
    }
    private void _storeReceivedXxxUTicket(UTicket receivedUTicket) {
        try {
            String receivedUTicketJson = UTicket.uTicketToJsonStr(receivedUTicket);

            // We store this UTicket in device_table["device_id"]
            if (receivedUTicket.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET) ||
                    receivedUTicket.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET)) {

                this.sharedData.getDeviceTable().put(
                        receivedUTicket.getDeviceId(),
                        new OtherDevice(
                                receivedUTicket.getDeviceId(),
                                receivedUTicketJson
                        )
                );
            } else { // Normally, we do not forward Initialization UTicket
                throw new RuntimeException("Shouldn't Reach Here");
            }

            // Storage
            this.simpleStorage.storeStorage(
                    this.sharedData.getThisDevice(),
                    this.sharedData.getDeviceTable(),
                    this.sharedData.getThisPerson(),
                    this.sharedData.getCurrentSession()
            );

        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }

    public void storeReceivedXxxRTicket(RTicket receivedRTicket) {
        SimpleMeasurer.measureWorkerFunc(this::_storeReceivedXxxRTicket, receivedRTicket);
    }
    private void _storeReceivedXxxRTicket(RTicket receivedRTicket) {
        try {
            String receivedRTicketJson = RTicket.rTicketToJsonStr(receivedRTicket);

            // We store this RTicket (but not verified) in deviceTable["device_id"]
            if (receivedRTicket.getRTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)) {
                // Holder (for Owner)
                String createdDeviceId = receivedRTicket.getDeviceId();
                // Put u_ticket (temporary in device_table["noId"]) & r_ticket in device_table["created_device_id"]
                this.sharedData.getDeviceTable().put(
                        createdDeviceId,
                        new OtherDevice(
                                createdDeviceId,
                                this.sharedData.getDeviceTable().get("noId").getDeviceUTicketForOwner(),
                                receivedRTicketJson
                        )
                );
            } else if (receivedRTicket.getRTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // Holder (for Owner)
                if (this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId()).getDeviceOwnershipUTicketForOthers() == null) {
                    // Not create new table, just add r_ticket to existing table
                    this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId())
                            .setDeviceRTicketForOwner(receivedRTicketJson);
                }
                // Issuer (for Others)
                else {
                    // Not create new table, just add r_ticket to existing table
                    this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId())
                            .setDeviceOwnershipRTicketForOthers(receivedRTicketJson);
                }
            } else if (receivedRTicket.getRTicketType().equals(UTicket.TYPE_ACCESS_END_UTOKEN)) {
                // Holder (for Owner)
                if (this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId()).getDeviceAccessUTicketForOthers() == null) {
                    // Not create new table, just add r_ticket to existing table
                    this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId())
                            .setDeviceRTicketForOwner(receivedRTicketJson);
                }
                // Issuer (for Others)
                else {
                    // Not create new table, just add r_ticket to existing table
                    this.sharedData.getDeviceTable().get(receivedRTicket.getDeviceId())
                            .setDeviceAccessEndRTicketForOthers(receivedRTicketJson);
                }
            } else {
                throw new RuntimeException("Shouldn't Reach Here");
            }

            // Storage
            this.simpleStorage.storeStorage(
                    this.sharedData.getThisDevice(),
                    this.sharedData.getDeviceTable(),
                    this.sharedData.getThisPerson(),
                    this.sharedData.getCurrentSession()
            );

        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }
}

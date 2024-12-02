package ureka.framework.logic.stage_worker;

import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.OtherDevice;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;
import ureka.framework.resource.storage.SimpleStorage;

public class GeneratedMsgStorer {
    private SharedData sharedData;
    private MeasureHelper measureHelper;
    private SimpleStorage simpleStorage;

    public GeneratedMsgStorer(SharedData sharedData, MeasureHelper measureHelper, SimpleStorage simpleStorage) {
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

    // [STAGE: (SG)] Store Generated Message
    public void storeGeneratedXxxUTicket(String generatedUTicketJson) {
        SimpleMeasurer.measureWorkerFunc(this::_storeGeneratedXxxUTicket, generatedUTicketJson);
    }
    private void _storeGeneratedXxxUTicket(String generatedUTicketJson) {
        try {
            // [STAGE: (VR)]
            UTicket generatedUTicket = UTicket.jsonStrToUTicket(generatedUTicketJson);

            /* Because device hasn't created the id yet,
               we temporary store Initialization UTicket in device_table["noId"]
               and the deviceTable will be updated by its RTicket with newly-created device_id
             */
            String device_idForUTicket;
            if (generatedUTicket.getUTicketType().equals(UTicket.TYPE_INITIALIZATION_UTICKET)) {
                // Holder (for Owner)
                device_idForUTicket = "noId";
                this.sharedData.getDeviceTable().put(device_idForUTicket, new OtherDevice(
                        device_idForUTicket, generatedUTicketJson));
            } else if (generatedUTicket.getUTicketType().equals(UTicket.TYPE_OWNERSHIP_UTICKET)) {
                // Issuer (for Others)
                // Not create new table, just add u_ticket to existing table
                device_idForUTicket = generatedUTicket.getDeviceId();
                OtherDevice device = this.sharedData.getDeviceTable().get(device_idForUTicket);
                System.out.println("OOOOOOOOOOOO");
                device.setDeviceOwnershipUTicketForOthers(generatedUTicketJson);
                System.out.println(device.getDeviceOwnershipUTicketForOthers());
            } else if (generatedUTicket.getUTicketType().equals(UTicket.TYPE_SELFACCESS_UTICKET)) {
                // Holder (for Owner)
                // Not create new table, just add u_ticket to existing table
                device_idForUTicket = generatedUTicket.getDeviceId();
                OtherDevice device = this.sharedData.getDeviceTable().get(device_idForUTicket);
                device.setDeviceUTicketForOwner(generatedUTicketJson);
            } else if (generatedUTicket.getUTicketType().equals(UTicket.TYPE_ACCESS_UTICKET)) {
                // Issuer (for Others)
                // Not create new table, just add u_ticket to existing table
                device_idForUTicket = generatedUTicket.getDeviceId();
                OtherDevice device = this.sharedData.getDeviceTable().get(device_idForUTicket);
                device.setDeviceAccessUTicketForOthers(generatedUTicketJson);
            } else {
                throw new RuntimeException("Not implemented yet");
            }

            // Storage
            this.simpleStorage.storeStorage(
                    this.sharedData.getThisDevice(),
                    this.sharedData.getDeviceTable(),
                    this.sharedData.getThisPerson(),
                    this.sharedData.getCurrentSession()
            );

        } catch (RuntimeException error) {
            this.sharedData.setResultMessage(error.getMessage());
            throw error;
        } catch (Exception e) {
            throw new RuntimeException("Shouldn't Reach Here", e);
        }
    }
}

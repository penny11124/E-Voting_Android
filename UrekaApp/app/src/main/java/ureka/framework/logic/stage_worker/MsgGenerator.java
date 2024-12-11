package ureka.framework.logic.stage_worker;

import java.util.Map;

import ureka.framework.model.SharedData;
import ureka.framework.model.message_model.RTicket;
import ureka.framework.model.message_model.UTicket;
import ureka.framework.resource.logger.SimpleLogger;
import ureka.framework.resource.logger.SimpleMeasurer;

public class MsgGenerator {
    private SharedData sharedData;
    private MeasureHelper measureHelper;

    public MsgGenerator(SharedData sharedData, MeasureHelper measureHelper) {
        this.sharedData = sharedData;
        this.measureHelper = measureHelper;
    }

    // [STAGE: (G)] Generate Message
    public String generateXxxUTicket(Map<String, String> arbitraryDict) {
        return SimpleMeasurer.measureWorkerFunc(this::_generateXxxUTicket, arbitraryDict);
    }
    private String _generateXxxUTicket(Map<String, String> arbitraryDict) {
        SimpleLogger.simpleLog("info",this.sharedData.getThisDevice().getDeviceName() + " is generating U Ticket...");

        MsgGeneratorUTicket uTicketGenerator = new MsgGeneratorUTicket(
                this.sharedData.getThisDevice(),
                this.sharedData.getThisPerson(),
                this.sharedData.getDeviceTable()
        );
        UTicket generatedUTicket;
        try {
            generatedUTicket = uTicketGenerator.generateArbitraryUTicket(arbitraryDict);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return UTicket.uTicketToJsonStr(generatedUTicket);
    }

    public String generateXxxRTicket(Map<String, String> arbitraryDict) {
        return SimpleMeasurer.measureWorkerFunc(this::_generateXxxRTicket, arbitraryDict);
    }
    private String _generateXxxRTicket(Map<String, String> arbitraryDict) {
        SimpleLogger.simpleLog("info",this.sharedData.getThisDevice().getDeviceName() + " is generating R Ticket...");

        MsgGeneratorRTicket rTicketGenerator = new MsgGeneratorRTicket(
                this.sharedData.getThisDevice(),
                this.sharedData.getThisPerson(),
                this.sharedData.getDeviceTable()
        );

        RTicket generatedRTicket;
        try {
            generatedRTicket = rTicketGenerator.generateArbitraryRTicket(arbitraryDict);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return RTicket.rTicketToJsonStr(generatedRTicket);
    }
}
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
        SimpleLogger.simpleLog("info",this.sharedData.getThisDevice().getDeviceName() + " is generating u_ticket...");

        MsgGeneratorUTicket uTicketGenerator = new MsgGeneratorUTicket(
                this.sharedData.getThisDevice(),
                this.sharedData.getThisPerson(),
                this.sharedData.getDeviceTable()
        );
        UTicket generatedUTicket = uTicketGenerator.generateArbitraryUTicket(arbitraryDict);
        String generatedUTicketJson = UTicket.uTicketToJsonStr(generatedUTicket);

        return generatedUTicketJson;
    }

    public String generateXxxRTicket(Map<String, String> arbitraryDict) {
        return SimpleMeasurer.measureWorkerFunc(this::_generateXxxRTicket, arbitraryDict);
    }

    private String _generateXxxRTicket(Map<String, String> arbitraryDict) {
        SimpleLogger.simpleLog("info",this.sharedData.getThisDevice().getDeviceName() + " is generating r_ticket...");

        MsgGeneratorRTicket rTicketGenerator = new MsgGeneratorRTicket(
                this.sharedData.getThisDevice(),
                this.sharedData.getThisPerson(),
                this.sharedData.getDeviceTable()
        );
        RTicket generatedRTicket = rTicketGenerator.generateArbitraryRTicket(arbitraryDict);
        String generatedRTicketJson = RTicket.rTicketToJsonStr(generatedRTicket);

        return generatedRTicketJson;
    }

}

package ureka.framework.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import ureka.framework.logic.stage_worker.MeasureHelper;
import ureka.framework.model.SharedData;
import ureka.framework.model.data_model.CurrentSession;
import ureka.framework.model.data_model.ThisDevice;
import ureka.framework.model.data_model.ThisPerson;
import ureka.framework.resource.crypto.ECDH;
import ureka.framework.resource.logger.SimpleLogger;

public class StageWorkerMeasureHelperTest {
    private MeasureHelper measureHelper;

    private void foo() {
        int counter = 0;
        for (int i = 0; i < 1e6; i++) {
            counter++;
        }
    }

    @BeforeEach
    public void init() {
        measureHelper = new MeasureHelper(new SharedData(new ThisDevice(), new CurrentSession(), new ThisPerson()));
        measureHelper.getSharedData().setMeasureRec(new HashMap<>());
    }

    @Test
    public void measureRecvCliPerfTimeTest() {
        measureHelper.measureProcessPerfStart();
        foo();
        measureHelper.measureRecvCliPerfTime("foo");
    }

    @Test
    public void measureRecvMsgPerfTimeTest() {
        measureHelper.measureProcessPerfStart();
        foo();
        measureHelper.measureRecvMsgPerfTime("foo");
    }

    @Test
    public void measureCommTimeTest() {
        measureHelper.measureCommPerfStart();
        foo();
        measureHelper.measureCommTime("foo");
    }

    @Test
    public void measureMessageSizeTest() throws Exception {
        String message = ECDH.generateRandomStr(1000000);
        measureHelper.measureMessageSize("foo", message);
    }
}

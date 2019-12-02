package com.chi.ssetest.cases;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.chi.ssetest.protos.SetupConfig;
import com.chi.ssetest.setup.RunnerSetup;
import com.chi.ssetest.StockTestcase;
import com.chi.ssetest.StockTestcaseName;
import com.chi.ssetest.setup.TestcaseConfigRule;
import com.mitake.core.bean.log.ErrorInfo;
import com.mitake.core.request.OrderQuantityRequest;
import com.mitake.core.response.IResponseInfoCallback;
import com.mitake.core.response.OrderQuantityResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *买卖队列1
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@StockTestcase(StockTestcaseName.ORDERQUANTITYTEST_1)
public class OrderQuantityTest_1 {
    private static final StockTestcaseName testcaseName = StockTestcaseName.ORDERQUANTITYTEST_1;
    private static SetupConfig.TestcaseConfig testcaseConfig;

    @BeforeClass

    public static void setup() throws Exception {
        Log.d(" OrderQuantityTest_1", "Setup");
        testcaseConfig = RunnerSetup.getInstance().getTestcaseConfig(testcaseName);
        if (testcaseConfig == null ) {
            throw new Exception(String.format("Testcase(%s) setup failed, config is empty", testcaseName));
        }
    }

    @Rule
    public TestcaseConfigRule rule = new TestcaseConfigRule(testcaseConfig);

    @Test(timeout = 5000)
    public void requestWork() throws Exception {
        Log.d("OrderQuantityTest_1", "requestWork");
        // TODO get custom args from param
        final String quoteNumbers = rule.getParam().optString("symbolID");
        final CompletableFuture result = new CompletableFuture<JSONObject>();
//        for (int i=0;i<quoteNumbers.length;i++){
           OrderQuantityRequest request = new OrderQuantityRequest();
            request.send(quoteNumbers,new IResponseInfoCallback<OrderQuantityResponse>() {
                @Override
                public void callback(OrderQuantityResponse orderQuantityResponse) {
                    try {
                        assertNotNull(orderQuantityResponse.list);
                    } catch (AssertionError e) {
                        result.completeExceptionally(e);
                    }
                    JSONObject uploadObj = new JSONObject();
                    try {
                        List<JSONObject> buylist=new ArrayList<>();
                        if (orderQuantityResponse.buyList!=null&&orderQuantityResponse.buyList.size()>0){
                            for (int k=0;k<orderQuantityResponse.buyList.size();k++){
                                JSONObject uploadObj_1 = new JSONObject();
//                               uploadObj_1.put("ID",orderQuantityResponse.buyList.get(k).ID_);
                                uploadObj_1.put("QUANTITY",orderQuantityResponse.buyList.get(k).QUANTITY_);
                                buylist.add(uploadObj_1);
                            }
                            uploadObj.put("buylist",new JSONArray(buylist));
                        }else {
                            uploadObj.put("buylist",orderQuantityResponse.buyList);
                        }

                        List<JSONObject> selllist=new ArrayList<>();
                        if (orderQuantityResponse.sellList!=null&&orderQuantityResponse.sellList.size()>0){
//                            for (int k=0;k<orderQuantityResponse.sellList.size();k++){
                            for (int k=orderQuantityResponse.sellList.size()-1;k>=0;k--){

                                if (orderQuantityResponse.sellList.get(k).QUANTITY_.size()!=0){
                                    JSONObject uploadObj_1 = new JSONObject();
//                                    uploadObj_1.put("ID",orderQuantityResponse.sellList.get(k).ID_);
                                    uploadObj_1.put("QUANTITY",orderQuantityResponse.sellList.get(k).QUANTITY_);
                                    selllist.add(uploadObj_1);
                                }
                            }
                            uploadObj.put("selllist",new JSONArray(selllist));
                        }else {
                            uploadObj.put("selllist",orderQuantityResponse.sellList);
                        }
//                        items.add(uploadObj);
                    } catch (JSONException e) {
                        result.completeExceptionally(e);
                    }
                    Log.d("data", String.valueOf(uploadObj));
                    result.complete(uploadObj);
                }
                @Override
                public void exception(ErrorInfo errorInfo) {
                    result.completeExceptionally(new Exception(errorInfo.toString()));
                }
            });
            try {
                JSONObject resultObj = (JSONObject)result.get(5000, TimeUnit.MILLISECONDS);
                RunnerSetup.getInstance().getCollector().onTestResult(testcaseName, rule.getParam(),resultObj);
            } catch (Exception e) {
                throw new Exception(e);
            }
//        }
    }
}

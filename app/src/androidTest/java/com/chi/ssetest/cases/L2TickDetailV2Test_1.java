package com.chi.ssetest.cases;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.chi.ssetest.StockTestcase;
import com.chi.ssetest.StockTestcaseName;
import com.chi.ssetest.protos.SetupConfig;
import com.chi.ssetest.setup.RunnerSetup;
import com.chi.ssetest.setup.TestcaseConfigRule;
import com.mitake.core.bean.TickDetailItem;
import com.mitake.core.bean.log.ErrorInfo;
import com.mitake.core.request.L2TickDetailRequestV2;
import com.mitake.core.response.IResponseInfoCallback;
import com.mitake.core.response.L2TickDetailResponseV2;
import com.mitake.core.response.Response;

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

import static org.junit.Assert.assertNotNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
//L2逐笔
@RunWith(AndroidJUnit4.class)
@StockTestcase(StockTestcaseName.L2TICKDETAILV2TEST_1)
public class L2TickDetailV2Test_1 {
    private static final StockTestcaseName testcaseName = StockTestcaseName.L2TICKDETAILV2TEST_1;
    private static SetupConfig.TestcaseConfig testcaseConfig;
    final CompletableFuture result = new CompletableFuture<JSONObject>();
    private static JSONObject uploadObj = new JSONObject();
    private static List<JSONObject> items=new ArrayList<>();
    @BeforeClass
    public static void setup() throws Exception {
        Log.d("L2TickDetailV2Test_1", "Setup");
        testcaseConfig = RunnerSetup.getInstance().getTestcaseConfig(testcaseName);
        if (testcaseConfig == null ) {
            throw new Exception(String.format("Testcase(%s) setup failed, config is empty", testcaseName));
        }
    }
    @Rule
    public TestcaseConfigRule rule = new TestcaseConfigRule(testcaseConfig);

    @Test(timeout = 5000)
    public void requestWork() throws Exception {
        Log.d("L2TickDetailV2Test_1", "requestWork");
        // TODO get custom args from param
        final String quoteNumbers = rule.getParam().optString("CODES", "");
        final String Pages = rule.getParam().optString("PAGES", "");
        final String SubTypes = rule.getParam().optString("SUBTYPES", "");

        L2TickDEtailjk(quoteNumbers,Pages,SubTypes);
        try {
            JSONObject resultObj = (JSONObject)result.get(5000, TimeUnit.MILLISECONDS);
            RunnerSetup.getInstance().getCollector().onTestResult(testcaseName,rule.getParam(), resultObj);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }
    private void L2TickDEtailjk(final String id, final String page, final String subtype) {
        L2TickDetailRequestV2 request = new L2TickDetailRequestV2();
        request.send(id,page,subtype, new IResponseInfoCallback() {
            @Override
            public void callback(Response response) {
                L2TickDetailResponseV2 l2TickDetailResponseV2 = (L2TickDetailResponseV2) response;
                List<TickDetailItem> list =l2TickDetailResponseV2.tickDetailItems;
                String[] str1=page.split(",");
                if (str1[2].equals("-1")){
                    try {
                        assertNotNull(l2TickDetailResponseV2.tickDetailItems);
                    } catch (AssertionError e) {
                        result.completeExceptionally(e);
                    }
                }
//                System.out.println(list.size()+"++++++++++++");
                if (list!=null){
                    try {
                        for (int k=0;k<list.size();k++){
                            JSONObject uploadObj_1 = new JSONObject();
                            uploadObj.put("code", id);
                            uploadObj_1.put("type", list.get(k).getTransactionStatus());
                            uploadObj_1.put("time", list.get(k).getTransactionTime());
                            uploadObj_1.put("tradeVolume", list.get(k).getSingleVolume());
                            uploadObj_1.put("tradePrice", list.get(k).getTransactionPrice());
                            items.add(uploadObj_1);
                        }
                    } catch (JSONException e) {
                        result.completeExceptionally(e);
                    }
                    if (list.size()==100){
                        String[] st=l2TickDetailResponseV2.headerParams.split(",");
                        if (Double.parseDouble(st[0])>Double.parseDouble(st[1])){
                            String page1=st[1]+",100,1";
                            L2TickDEtailjk(id,page1,subtype);
                        }else {
                            String page2=st[0]+",100,1";
                            L2TickDEtailjk(id,page2,subtype);
                        }
                    }else {
                        try {
                            uploadObj.put("items",new JSONArray(items));
                            JSONArray jsonArray = uploadObj.getJSONArray("items");
//                            System.out.println(jsonArray.getJSONObject(jsonArray.length()-1));
//                            System.out.println(jsonArray.length()+"++++++++++++");
                            for (int i=0;i<jsonArray.length();i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.d("data", String.valueOf(jsonObject));
                            }
                        } catch (JSONException e) {
                            result.completeExceptionally(e);
                        }
//                        Log.d("data", String.valueOf(uploadObj));
                        result.complete(uploadObj);
                    }
                }else {
                    try {
                        uploadObj.put("items",new JSONArray(items));
                        JSONArray jsonArray = uploadObj.getJSONArray("items");
//                        System.out.println(jsonArray.getJSONObject(jsonArray.length()-1));
//                        System.out.println(jsonArray.length()+"++++++++++++");
                        for (int i=0;i<jsonArray.length();i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            Log.d("data", String.valueOf(jsonObject));
                        }
                    } catch (JSONException e) {
                        result.completeExceptionally(e);
                    }
//                    Log.d("data", String.valueOf(uploadObj));
                    result.complete(uploadObj);
                }
            }
            @Override
            public void exception(ErrorInfo errorInfo) {
                result.completeExceptionally(new Exception(errorInfo.toString()));
            }
        });
    }
}

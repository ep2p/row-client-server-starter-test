package lab.idioglossia.row.cs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lab.idioglossia.row.client.RowClient;
import lab.idioglossia.row.client.RowClientFactory;
import lab.idioglossia.row.client.RowMessageHandlerProvider;
import lab.idioglossia.row.client.callback.GeneralCallback;
import lab.idioglossia.row.client.callback.ResponseCallback;
import lab.idioglossia.row.client.callback.RowTransportListener;
import lab.idioglossia.row.client.model.RowRequest;
import lab.idioglossia.row.client.model.RowResponse;
import lab.idioglossia.row.client.registry.MapCallbackRegistry;
import lab.idioglossia.row.client.tyrus.ConnectionRepository;
import lab.idioglossia.row.client.tyrus.RowClientConfig;
import lab.idioglossia.row.client.util.DefaultJacksonMessageConverter;
import lab.idioglossia.row.client.ws.SpringRowWebsocketClient;
import lab.idioglossia.row.client.ws.WebsocketSession;
import lab.idioglossia.row.server.annotations.RowController;
import lab.idioglossia.row.server.annotations.RowIgnore;
import lab.idioglossia.row.server.context.RowContext;
import lab.idioglossia.row.server.context.RowContextHolder;
import lab.idioglossia.row.server.repository.RowSessionRegistry;
import lab.idioglossia.row.server.ws.SpringRowServerWebsocket;
import lombok.SneakyThrows;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.CloseReason;
import java.util.HashMap;
import java.util.Map;

@RowController
public class EndpointController {
    private final RowClientFactory rowClientFactory;
    private final SpringReuseRowClientFactory springReuseRowClientFactory;
    private final RowSessionRegistry rowSessionRegistry;

    @Autowired
    public EndpointController(RowClientFactory rowClientFactory, SpringReuseRowClientFactory springReuseRowClientFactory, RowSessionRegistry rowSessionRegistry) {
        this.rowClientFactory = rowClientFactory;
        this.springReuseRowClientFactory = springReuseRowClientFactory;
        this.rowSessionRegistry = rowSessionRegistry;
    }

    @SneakyThrows
    @RowIgnore
    @GetMapping("/api/run")
    public String run(@RequestParam(value = "port", defaultValue = "8081", required = false) String port){
        RowClientConfig rowClientConfig = rowClientFactory.getRowClientConfig();
        rowClientConfig.setAddress("ws://127.0.0.1:" + port + "/ws");
        RowClient rowClient = rowClientFactory.getRowClient(rowClientConfig);
        rowClient.open();

        RowRequest<SampleDto, Object> rowRequest = RowRequest.getDefault("/api/data", RowRequest.RowMethod.POST);
        rowRequest.setBody(new SampleDto("input", "test data"));
        rowClient.sendRequest(rowRequest, new ResponseCallback<SampleDto>(SampleDto.class) {
            @Override
            public void onResponse(RowResponse<SampleDto> rowResponse) {
                System.out.println("Response from api" + rowResponse.getBody().getData());
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Failed to get data");
                throwable.printStackTrace();
            }
        });

        return "ok";
    }

    @SneakyThrows
    @PostMapping("/api/data")
    public @ResponseBody
    SampleDto dataTest(@RequestBody SampleDto sampleDto){
        System.out.println(sampleDto.getData());
        SampleDto output = new SampleDto("output", sampleDto.getData() + " received");
        RowContext context = RowContextHolder.getContext();
        if (context.isRowRequest()) {
            try {
                SpringRowServerWebsocket rowWebsocketSession = (SpringRowServerWebsocket) rowSessionRegistry.getSession(context.getRowUser().getUserId(), context.getRowUser().getSessionId());
                RowClientConfig rowClientConfig = springReuseRowClientFactory.getRowClientConfig();
                rowClientConfig.setRowTransportListener(new RowTransportListener() {
                    @Override
                    public void onOpen(WebsocketSession websocketSession) {

                    }

                    @Override
                    public void onError(WebsocketSession websocketSession, Throwable throwable) {

                    }

                    @Override
                    public void onClose(RowClient rowClient, WebsocketSession websocketSession, CloseReason closeReason) {
                        System.out.println("Transport listener onClose() is working.");
                    }
                });
                RowClient rowClient = springReuseRowClientFactory.getRowClient(rowClientConfig, rowWebsocketSession);
                RowRequest<SampleDto, Object> rowRequest = RowRequest.getDefault("/api/reused", RowRequest.RowMethod.POST);
                rowRequest.setBody(new SampleDto("input", "reuse websocket"));
                rowClient.sendRequest(rowRequest, new ResponseCallback<SampleDto>(SampleDto.class) {
                    @SneakyThrows
                    @Override
                    public void onResponse(RowResponse<SampleDto> rowResponse) {
                        System.out.println("Response from api" + rowResponse.getBody().getData());
                        Thread.sleep(5000);
                        rowClient.close();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
                System.out.println("Sent message");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return output;
    }

    @SneakyThrows
    @PostMapping("/api/reused")
    public @ResponseBody SampleDto reuses(){
        System.out.println("Other server reused the websocket");
        return new SampleDto("output", "nice");
    }

    @ToString
    public static class SampleDto {
        private Map<String, String> data;

        public SampleDto(String key, String value){
            this.data = new HashMap<>();
            this.data.put(key, value);
        }

        public SampleDto() {
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }
}

package za.co.wethinkcode.protocol.client;

import com.google.gson.JsonObject;
import za.co.wethinkcode.client.commands.LookCommand;
import za.co.wethinkcode.client.commands.StateCommand;

/**
 * Represents a response containing the robot's vision and state information.
 * Constructs a JSON response encapsulating the robot's environment (vision) and current state.
 */

public class LookResponse {
    private final LookCommand vision;
    private final StateCommand state;

    /**
     * Initializes a LookResponse with vision and state data.
     *
     * @param vision the LookCommand containing the robot's vision
     * @param state the StateCommand containing the robot's state
     */

    public LookResponse(LookCommand vision, StateCommand state) {
        this.vision = vision;
        this.state = state;
    }

    /**
     * Converts this LookResponse into a JSON object.
     * The JSON contains a "result" property with "OK",
     * a "data" property with vision details, and a "state" property with state details.
     *
     * @return a JsonObject representing this LookResponse
     */

    public JsonObject toJson() {
        JsonObject response = new JsonObject();
        response.addProperty("result", "OK");
        response.add("data", vision.toJson());
        response.add("state", state.toJson());
        return response;
    }
}
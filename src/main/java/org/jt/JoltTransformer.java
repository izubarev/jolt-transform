package org.jt;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;


public class JoltTransformer
{
    public static void main(String[] args) throws IOException {

        if (args.length < 3) {
            System.out.println("Usage: ./jolt-transform <input> <spec> <specName>");
            return;
        }

        var jsonEntities = parseJsonFile(args[0]).get("exportResponse").getAsJsonObject().get("entities").getAsJsonArray();
        var joltSpec = parseJsonFile(args[1]).get(args[2]).getAsJsonArray();

        JsonObject result = new JsonObject();
        JsonObject body = new JsonObject();
        JsonArray rootEntities = new JsonArray();
        result.add("body", body);
        body.add("entities", rootEntities);

        var chainr = Chainr.fromSpec(JsonUtils.jsonToList(joltSpec.toString()));
        var gson = new GsonBuilder().create();

        for (var entity: jsonEntities) {
            var wrap = new JsonArray();
            wrap.add(entity);
            var jsonSource = JsonUtils.jsonToList(wrap.toString());
            var transformed = chainr.transform(jsonSource);
            rootEntities.add(gson.toJsonTree(transformed));
        }

        System.out.println(gson.toJson(result));
    }

    private static JsonObject parseJsonFile(String path) throws IOException {
        try(var reader = new FileReader(Paths.get(path).toFile())) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

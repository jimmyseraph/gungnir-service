package vip.testops.gungnir.converters;

import org.bson.Document;
import org.jacoco.core.data.ExecutionData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.ArrayList;

@ReadingConverter
public class ExecutionDataReadConverter implements Converter<Document, ExecutionData> {
    @Override
    public ExecutionData convert(Document document) {
        ArrayList<Boolean> list = (ArrayList<Boolean>) document.get("probes");
        boolean[] probes = new boolean[list.size()];
        for(int i = 0; i < list.size(); i++){
            probes[i] = list.get(i);
        }
        return new ExecutionData((Long)document.get("_id"), (String)document.get("name"), probes);
    }
}

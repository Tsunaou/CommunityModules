package tlc2.overrides;


import tlc2.output.EC;
import tlc2.tool.EvalException;
import tlc2.value.IValue;
import tlc2.value.Values;
import tlc2.value.impl.*;

import java.io.*;
import java.util.ArrayList;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONReader;

public class PartialOrderExt {

    /**
     * @param s : A set
     * @param PODirectory : a work directory of POFiles, the file you can found in https://drive.google.com/drive/folders/1Ut0lyLorh4MERIlJCtYTpaGCyerqUvrA?usp=sharing
     * @return A set of all subset of the Cartesian Product of s and s itself(or say s \time s),
     *          each of which represent a strict partial order(irreflexive and transitive)
     */
    @TLAPlusOperator(identifier = "PartialOrderSubset", module = "PartialOrderExt", warn = false)
    public static SetEnumValue partialOrderSubset(final Value s, final StringValue PODirectory) {
        final SetEnumValue set = (SetEnumValue) s.toSetEnum();
        // Get all elements in set s and store them into an array of Value
        Value[] elems = set.elems.toArray();
        // Get all possible partial order in {0,1,2,...n-1} \times {0,1,2,...n-1}
        final int n = set.size();
        final String filepath = PODirectory.toUnquotedString() + "strictPartialOrder" + n + ".json";
        System.out.println("Reading JSON file from " + filepath);


        // Get all possible partial order in s \times s
        ValueVec subset = new ValueVec();

        JSONReader reader = null;
        try {
            reader = new JSONReader(new FileReader(filepath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert reader != null;
        reader.startArray();
        while (reader.hasNext()) {
            JSONArray po = (JSONArray) reader.readObject();
            if (po.size() > 0) {
                ValueVec subVec = new ValueVec();
                for (Object o : po) {
                    JSONArray rel = (JSONArray) o;
                    int i = (Integer) rel.get(0);
                    int j = (Integer) rel.get(1);
                    subVec.addElement(new TupleValue(elems[i], elems[j]));
                }
                subset.addElement(new SetEnumValue(subVec, true));
            }
        }

        reader.endArray();
        reader.close();

        return new SetEnumValue(subset, true);
    }

    private static void prettyPrint(IValue v) {
        System.out.println(Values.ppr(v));
    }

    public static void testExmaple() {
        String jsonfile = "D:\\Education\\Programs\\Python\\EnumeratePO\\POFile\\";
        StringValue path = new StringValue(jsonfile);
        ValueVec vec = new ValueVec();
        vec.addElement(IntValue.gen(0));
        vec.addElement(IntValue.gen(1));
        vec.addElement(IntValue.gen(2));
        vec.addElement(IntValue.gen(3));
        vec.addElement(IntValue.gen(4));
        vec.addElement(IntValue.gen(5));
        vec.addElement(IntValue.gen(6));
        SetEnumValue set = new SetEnumValue(vec, true);
        PartialOrderExt.prettyPrint(set);

        SetEnumValue po = (SetEnumValue) PartialOrderExt.partialOrderSubset(set, path);
        System.out.println(po.size());
//        PartialOrderExt.prettyPrint(po);
    }

    public static void main(String[] args) {
        testExmaple();

    }
}

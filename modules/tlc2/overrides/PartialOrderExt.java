package tlc2.overrides;


import tlc2.output.EC;
import tlc2.tool.EvalException;
import tlc2.value.IValue;
import tlc2.value.Values;
import tlc2.value.impl.*;

import java.io.*;
import java.util.ArrayList;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class PartialOrderExt {

    @TLAPlusOperator(identifier = "PartialOrderSubset", module = "PartialOrderExt", warn = false)
    public static SetEnumValue partialOrderSubset(final Value s, final StringValue poPath) {
        /***
         * A set whose element is a poset
         *
         */
        final SetEnumValue set = (SetEnumValue) s.toSetEnum();
        final String filepath = poPath.toUnquotedString();
        System.out.println("Reading JSON file from " + filepath);
        if (set == null) {
            throw new EvalException(EC.TLC_MODULE_ONE_ARGUMENT_ERROR,
                    new String[]{"first", "partialOrderSubset", "set", Values.ppr(s.toString())});
        }

        // Get all elements in set s and store them into an array of Value
        Value[] elems = set.elems.toArray();

        // Get all possible partial order in {0,1,2,...n-1} \times {0,1,2,...n-1}
        final int n = set.size();
        ArrayList<boolean[][]> pos = PartialRelationOf(filepath, n);

        // Get all possible partial order in s \times s
        ValueVec subset = new ValueVec();
        for (boolean[][] po : pos) {
            ValueVec subVec = new ValueVec();
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (po[i][j]) {
                        subVec.addElement(new TupleValue(elems[i], elems[j]));
                    }
                }
            }
            subset.addElement(new SetEnumValue(subVec, true));
        }

        SetEnumValue res = new SetEnumValue(subset, true);
        return res;
    }

    private static void prettyPrint(IValue v) {
        System.out.println(Values.ppr(v));

    }

    public static String readJsonFile(String fileName) {
        String jsonStr = "";
        try {
            File jsonFile = new File(fileName);
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile), "utf-8");
            int ch = 0;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonStr = sb.toString();
            return jsonStr;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ArrayList<boolean[][]> PartialRelationOf(String filepath, int n) {
        ArrayList<boolean[][]> res = new ArrayList<>();
        String s = readJsonFile(filepath);
        JSONObject strictPartialOrder = JSON.parseObject(s);
        JSONArray POs = strictPartialOrder.getJSONArray(String.valueOf(n));//构建JSONArray数组
        for (Object po : POs) {
            JSONArray PO = (JSONArray) po;
            if (PO.size() == 0) {
                // 空关系
                System.out.println("Empty Relation");
            } else {
                boolean[][] relation = new boolean[n][n];
                int x, y;
                for (Object o : PO) {
                    JSONArray rel = (JSONArray) o;
                    if (rel.size() != 2) {
                        System.exit(-1);
                    } else {
                        x = (Integer) rel.get(0);
                        y = (Integer) rel.get(1);
                        relation[x][y] = true;
                    }
                }
                res.add(relation);
            }
        }
        return res;
    }

    private static ArrayList<boolean[][]> fakeRelation2(int n) {
//        枚举只有2个操作所有可能的partial order
        ArrayList<boolean[][]> res = new ArrayList<>();
        // No.1
        boolean[][] res1 = new boolean[2][2];
        res1[0][0] = true;
        res1[1][1] = true;
        res.add(res1);
        // No.2
        boolean[][] res2 = new boolean[2][2];
        res2[0][0] = true;
        res2[1][0] = true;
        res2[1][1] = true;
        res.add(res2);
        // No.3
        boolean[][] res3 = new boolean[2][2];
        res3[0][0] = true;
        res3[0][1] = true;
        res3[1][1] = true;
        res.add(res3);

        return res;
    }

    public static void testExmaple() {
        String jsonfile = "D:\\Education\\Programs\\Python\\EnumeratePO\\strictPo0-4.json";
        StringValue path = new StringValue(jsonfile);
        ValueVec vec = new ValueVec();
        vec.addElement(IntValue.gen(0));
        vec.addElement(IntValue.gen(1));
        vec.addElement(IntValue.gen(2));
        SetEnumValue set = new SetEnumValue(vec, true);
        PartialOrderExt.prettyPrint(set);

        SetEnumValue po = (SetEnumValue) PartialOrderExt.partialOrderSubset(set, path);
        PartialOrderExt.prettyPrint(po);
    }


    public static void main(String[] args) {
        testExmaple();

    }
}

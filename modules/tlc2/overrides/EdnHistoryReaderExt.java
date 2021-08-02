package tlc2.overrides;

import DifferentiatedHistory.History;
import DifferentiatedHistory.HistoryItem;
import DifferentiatedHistory.HistoryReader;
import tlc2.value.IValue;
import tlc2.value.Values;
import tlc2.value.impl.*;
import util.InternTable;
import util.UniqueString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


/*
    hasa == <<W("x", 1, 1), R("x", 2, 2)>>
    hasb == <<W("x", 2, 3), R("x", 1, 4)>>
    ha == {hasa, hasb} \* CM but not CCv

    A history is a set of tuples.
    Each element in a tuple is an operation.
    An operation is a function.
 */

public class EdnHistoryReaderExt {

    final static InternTable internTable = new InternTable(3);
    final static UniqueString type = internTable.put("type");
    final static UniqueString key = internTable.put("key");
    final static UniqueString value = internTable.put("value");
    final static UniqueString[] names = {type, key, value};

    public static RecordValue newOperation(String type, String key, int value, int oid) {
        StringValue recOpType = new StringValue(type);
        StringValue recKey = new StringValue(key);
        IntValue recValue = IntValue.gen(value);
        IntValue recOid = IntValue.gen(oid);
        Value[] values = {recOpType, recKey, recValue, recOid};
        return new RecordValue(names, values, false);
    }

    @TLAPlusOperator(identifier = "EdnHistoryReader", module = "EdnHistoryReaderExt", warn = false)
    public static SetEnumValue ednHistoryReaderExt(final StringValue ednHistoryFile) {
        HistoryReader reader = new HistoryReader(ednHistoryFile.toUnquotedString(), 10, true);
        try {
            History history = reader.readHistory(99999);
            // Group operations by process
            HashMap<Integer, ArrayList<HistoryItem>> historyByProcess = new HashMap<>();
            for (HistoryItem op : history.getOperations()) {
                int process = op.getProcess();
                if (!historyByProcess.containsKey(process)) {
                    historyByProcess.put(process, new ArrayList<>());
                }
                historyByProcess.get(process).add(op);
            }

            ArrayList<TupleValue> sessionList = new ArrayList<>();
            for (Map.Entry<Integer, ArrayList<HistoryItem>> entry : historyByProcess.entrySet()) {
                Value operations[] = new Value[entry.getValue().size()];
                int i = 0;
                for (HistoryItem op : entry.getValue()) {
                    String readOrWrite = "read";
                    if (op.isWrite()) {
                        readOrWrite = "write";
                    }
                    RecordValue operation = EdnHistoryReaderExt.newOperation(readOrWrite, op.getK(), op.getV(), op.getIndex());
                    operations[i] = operation;
                    i = i + 1;
                }
                TupleValue session = new TupleValue(operations);
                sessionList.add(session);
            }
            ValueVec vec = new ValueVec();
            for (TupleValue session : sessionList) {
                vec.addElement(session);
            }

            SetEnumValue res = new SetEnumValue(vec, true);
            return res;

        } catch (IOException e) {
            e.printStackTrace();
        }
        SetEnumValue res = new SetEnumValue();
        return res;
    }

    private static void prettyPrint(IValue v) {
        System.out.println(Values.ppr(v));
    }

    public static void main(String[] args) {
        String ednfile = "D:\\Education\\Programs\\Java\\Causal-Memory-Checking-Java\\src\\main\\resources\\adhoc\\paper_history_a.edn";
        StringValue path = new StringValue(ednfile);
        EdnHistoryReaderExt.prettyPrint(ednHistoryReaderExt(path));
    }
}

package syrincs.c_adapters.osc;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

final class OscMessageBuilder {
    private OscMessageBuilder() {
    }

    static byte[] build(String address, Object... arguments) {
        if (address == null || !address.startsWith("/")) {
            throw new IllegalArgumentException("OSC address must start with '/'");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeOscString(out, address);
        writeOscString(out, typeTags(arguments));

        for (Object argument : arguments) {
            if (argument instanceof String value) {
                writeOscString(out, value);
            } else if (argument instanceof Integer value) {
                writeInt(out, value);
            } else if (argument instanceof Float value) {
                writeFloat(out, value);
            } else if (argument instanceof Double value) {
                writeFloat(out, value.floatValue());
            } else {
                throw new IllegalArgumentException("Unsupported OSC argument type: " + argument);
            }
        }

        return out.toByteArray();
    }

    private static String typeTags(Object[] arguments) {
        StringBuilder tags = new StringBuilder(",");
        for (Object argument : arguments) {
            if (argument instanceof String) {
                tags.append('s');
            } else if (argument instanceof Integer) {
                tags.append('i');
            } else if (argument instanceof Float || argument instanceof Double) {
                tags.append('f');
            } else {
                throw new IllegalArgumentException("Unsupported OSC argument type: " + argument);
            }
        }
        return tags.toString();
    }

    private static void writeOscString(ByteArrayOutputStream out, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.write(bytes, 0, bytes.length);
        out.write(0);

        int lengthWithTerminator = bytes.length + 1;
        int padding = (4 - (lengthWithTerminator % 4)) % 4;
        for (int i = 0; i < padding; i++) {
            out.write(0);
        }
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >>> 24) & 0xff);
        out.write((value >>> 16) & 0xff);
        out.write((value >>> 8) & 0xff);
        out.write(value & 0xff);
    }

    private static void writeFloat(ByteArrayOutputStream out, float value) {
        writeInt(out, Float.floatToIntBits(value));
    }
}

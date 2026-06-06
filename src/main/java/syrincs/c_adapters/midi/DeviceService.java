package syrincs.c_adapters.midi;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

/**
 * Centralized device resolution helper for adapter layer.
 *
 * Semantics:
 * - listOutputInfos(): returns all MIDI devices that can receive (OUT) messages
 * - findOutputInfoBySubstring(): case-insensitive substring match over name/description/vendor
 * - autoSelectDefaultOutput(): preferred brand hints, then first OUT
 * - resolveOutput(substring): if substring provided → find by substring; else → autoSelectDefaultOutput
 */
public final class DeviceService {

    private DeviceService() {}

    private static final String MIDI_DEVICE_ENV = "SYRINCS_MIDI_DEVICE";

    static MidiDevice.Info[] listOutputInfos() {
        MidiDevice.Info[] all = MidiSystem.getMidiDeviceInfo();
        java.util.List<MidiDevice.Info> outs = new java.util.ArrayList<>();
        for (MidiDevice.Info info : all) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                int maxReceivers = dev.getMaxReceivers();
                if (maxReceivers != 0) { // -1 unlimited or >0
                    outs.add(info);
                }
            } catch (MidiUnavailableException ignored) {
            }
        }
        return outs.toArray(new MidiDevice.Info[0]);
    }

    static MidiDevice.Info findOutputInfoBySubstring(String nameSubstring) {
        if (nameSubstring == null || nameSubstring.isBlank()) return null;
        String needle = nameSubstring.toLowerCase();
        for (MidiDevice.Info info : listOutputInfos()) {
            String hay = (info.getName() + " " + info.getDescription() + " " + info.getVendor()).toLowerCase();
            if (hay.contains(needle)) return info;
        }
        return null;
    }

    static MidiDevice.Info autoSelectDefaultOutput() {
        String[] needles = {"Roland Digital Piano", "DP603"};
        for (String n : needles) {
            MidiDevice.Info info = findOutputInfoBySubstring(n);
            if (info != null) return info;
        }
        // Fallback: first OUT
        MidiDevice.Info[] outs = listOutputInfos();
        return outs.length > 0 ? outs[0] : null;
    }

    static MidiDevice.Info resolveOutput(String deviceNameSubstring) {
        if (deviceNameSubstring != null && !deviceNameSubstring.isBlank()) {
            return findOutputInfoBySubstring(deviceNameSubstring);
        }

        String envDevice = System.getenv(MIDI_DEVICE_ENV);
        if (envDevice != null && !envDevice.isBlank()) {
            return findOutputInfoBySubstring(envDevice);
        }

        return autoSelectDefaultOutput();
    }
}

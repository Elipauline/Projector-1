package org.noisesmith.projector;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;

public class FootPedal {
    public FootPedal(String match, SynchronousQueue queue) {
        MidiDevice dev;
        MidiReceiver receive;
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < infos.length; i++) {
            if(infos[i].getName().matches(match))
                try {
                    dev = MidiSystem.getMidiDevice(infos[i]);
                    System.out.println("opening " + infos[i].getName());
                    Transmitter trans = dev.getTransmitter();
                    receive = new MidiReceiver(dev.getDeviceInfo().toString(),
                                               queue);
                    trans.setReceiver(receive);
                    dev.open();
                    System.out.println(dev.getDeviceInfo()+" was opened");
                } catch (javax.sound.midi.MidiUnavailableException e) {
                    System.out.println("error opening device " +
                                       infos[i] + " for input");
                }
        }
    }

    public enum Events {
        FOOT_DOWN,
        FOOT_UP,
        NONE
    }

    public class Event {
        public final Events type;
        public final int parameter;

        Event(byte[] midi) {
            byte eVtype = midi[0];
            byte eVindex = midi[1];
            byte eVparameter = midi[2];
            switch (eVtype) {
            case -112:
                parameter = eVindex -59;
                if(eVparameter == 0)
                    type = Events.FOOT_UP;
                else
                    type = Events.FOOT_DOWN;
                break;
            default:
                type = Events.NONE;
                parameter = -1;
                System.out.print("... ignoring midi event: ");
                System.out.println("type " + eVtype + " index " + eVindex +
                                   " parameter " + eVparameter);
            }
        }
        @Override
        public String toString() {
            return "Event{" + type.toString() + ' ' + parameter + "}";
        }
    }

    class MidiReceiver implements Receiver {
        String name;
        SynchronousQueue queue;

        public MidiReceiver(String n, SynchronousQueue s) {
            name = n;
            queue = s;
        }

        public void send(MidiMessage msg, long timeStamp) {
            byte[] message = msg.getMessage();
            Event event = new Event(message);
            System.out.println("Event: " + event);
            try {
                queue.put(event);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void close() {}
    }
}

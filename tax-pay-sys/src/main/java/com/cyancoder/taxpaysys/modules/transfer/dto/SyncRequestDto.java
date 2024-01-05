package com.cyancoder.taxpaysys.modules.transfer.dto;

public class SyncRequestDto extends SignedRequest {

    private PacketDto packet;

    public PacketDto getPacket() {
        return packet;
    }

    public void setPacket(PacketDto packet) {
        this.packet = packet;
    }
}

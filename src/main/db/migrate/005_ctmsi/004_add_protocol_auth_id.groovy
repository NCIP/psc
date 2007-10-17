class AddProtocolAuthId extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "protocol_authority_id", "string")
    }

    void down() {
        dropColumn("studies", "protocol_authority_id")
    }
}
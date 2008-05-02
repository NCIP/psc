class AddProtocolAuthId extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        addColumn("studies", "protocol_authority_id", "string", limit: 900)
    }

    void down() {
        dropColumn("studies", "protocol_authority_id")
    }
}

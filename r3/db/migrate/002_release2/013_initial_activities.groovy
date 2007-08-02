class PersistentConfiguration extends edu.northwestern.bioinformatics.bering.Migration {
    void up() {
        execute("DELETE FROM activity_types");
        // do manual inserts to get explicit IDs on all platforms
        execute("INSERT INTO activity_types (id, name) VALUES (1, 'Disease Measure')");
        execute("INSERT INTO activity_types (id, name) VALUES (2, 'Intervention')");
        execute("INSERT INTO activity_types (id, name) VALUES (3, 'Lab Test')");
        execute("INSERT INTO activity_types (id, name) VALUES (4, 'Procedure')");
        execute("INSERT INTO activity_types (id, name) VALUES (5, 'Other')");

        insert("activities", [ activity_type_id: 1, name: "CT" ])
        insert("activities", [ activity_type_id: 1, name: "X-Ray" ])
        insert("activities", [ activity_type_id: 1, name: "MRI" ])
        insert("activities", [ activity_type_id: 1, name: "Bone Scan" ])
        insert("activities", [ activity_type_id: 1, name: "Mammogram" ])
        insert("activities", [ activity_type_id: 1, name: "Ultrasound" ])
        insert("activities", [ activity_type_id: 1, name: "CXR" ])
        insert("activities", [ activity_type_id: 1, name: "CSF cytology" ])
        insert("activities", [ activity_type_id: 1, name: "Tumor Measurement" ])

        insert("activities", [ activity_type_id: 2, name: "Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Capecitabine" ])
        insert("activities", [ activity_type_id: 2, name: "Docetaxel" ])
        insert("activities", [ activity_type_id: 2, name: "Peptide Vaccine" ])
        insert("activities", [ activity_type_id: 2, name: "Cytoxan Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Adriamycin Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Taxol Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Infusion CPT-11" ])
        insert("activities", [ activity_type_id: 2, name: "PBSCT" ])
        insert("activities", [ activity_type_id: 2, name: "IL-2" ])
        insert("activities", [ activity_type_id: 2, name: "Thalidomide daily" ])
        insert("activities", [ activity_type_id: 2, name: "Cyclosphosphamide Tablets" ])
        insert("activities", [ activity_type_id: 2, name: "Methotrexate Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Fluorouracil Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Cyclosphosphamide Infusion" ])
        insert("activities", [ activity_type_id: 2, name: "Capecitabine Tablets" ])

        insert("activities", [ activity_type_id: 3, name: "CBC" ])
        insert("activities", [ activity_type_id: 3, name: "CK" ])
        insert("activities", [ activity_type_id: 3, name: "CMP" ])
        insert("activities", [ activity_type_id: 3, name: "Plates" ])
        insert("activities", [ activity_type_id: 3, name: "PT/PTT" ])
        insert("activities", [ activity_type_id: 3, name: "ANA" ])
        insert("activities", [ activity_type_id: 3, name: "calcium" ])
        insert("activities", [ activity_type_id: 3, name: "Rheumatoid factor" ])
        insert("activities", [ activity_type_id: 3, name: "BhCG" ])
        insert("activities", [ activity_type_id: 3, name: "Creatinine kinase" ])
        insert("activities", [ activity_type_id: 3, name: "Urinalysis" ])
        insert("activities", [ activity_type_id: 3, name: "Albumin" ])
        insert("activities", [ activity_type_id: 3, name: "uric acid" ])

        insert("activities", [ activity_type_id: 4, name: "EKG" ])
        insert("activities", [ activity_type_id: 4, name: "DTH skin test" ])
        insert("activities", [ activity_type_id: 4, name: "Physical Exam" ])
        insert("activities", [ activity_type_id: 4, name: "Bone Marrow Biopsy" ])
        insert("activities", [ activity_type_id: 4, name: "MUGA or ECHO" ])
        insert("activities", [ activity_type_id: 4, name: "Height, Weight, Perf Status" ])
        insert("activities", [ activity_type_id: 4, name: "Vital signs" ])
        insert("activities", [ activity_type_id: 4, name: "LVEF" ])
        insert("activities", [ activity_type_id: 4, name: "Eye Exam" ])

        insert("activities", [ activity_type_id: 5, name: "Quality of Life (QOL)" ])
        insert("activities", [ activity_type_id: 5, name: "Mini Mental State Exam (MMSE)" ])
        insert("activities", [ activity_type_id: 5, name: "Diary Entry" ])
        insert("activities", [ activity_type_id: 5, name: "Brief Pain Inventory" ])
        insert("activities", [ activity_type_id: 5, name: "12 minute walk" ])
        insert("activities", [ activity_type_id: 5, name: "Rest 14 days" ])
        insert("activities", [ activity_type_id: 5, name: "Rest 4 weeks" ])
        insert("activities", [ activity_type_id: 5, name: "Electronic Telephone Diary" ])
    }

    void down() {
        execute("DELETE FROM activities");
        execute("DELETE FROM activity_types");
    }
}

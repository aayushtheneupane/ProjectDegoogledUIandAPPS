package com.google.i18n.phonenumbers;

interface MetadataSource {
    Phonemetadata$PhoneMetadata getMetadataForNonGeographicalRegion(int i);

    Phonemetadata$PhoneMetadata getMetadataForRegion(String str);
}

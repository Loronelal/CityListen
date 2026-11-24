package com.citylisten.domain.result

class LocationUnavailableException :
    IllegalStateException("Location is unavailable on this device")

package com.cyancoder.taxpaysys.modules.transfer.interfaces;

import java.util.Map;

public interface Normalizer {

    String normalize(Object data, Map<String, String> headers);
}

package org.theunnameddude.crossinv;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CrossInventory {
    @Getter
    @NonNull
    byte[] content;
}

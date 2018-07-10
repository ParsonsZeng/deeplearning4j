/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package org.nd4j.linalg.jcublas;

import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.ndarray.BaseSparseInfoProvider;
import org.nd4j.linalg.api.shape.Shape;
import org.nd4j.linalg.api.shape.SparseDescriptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Audrey Loeffel
 */
public class DirectSparseInfoProvider extends BaseSparseInfoProvider {

    private Map<SparseDescriptor, DataBuffer> sparseCache = new ConcurrentHashMap<>();
    private AtomicInteger counter = new AtomicInteger(0);
    private static final int MAX_ENTRIES = 100;

    @Override
    public DataBuffer createSparseInformation(int[] flags, long[] sparseOffsets, int[] hiddenDimensions, int underlyingRank) {

        SparseDescriptor descriptor = new SparseDescriptor(flags, sparseOffsets, hiddenDimensions, underlyingRank);

        if(!sparseCache.containsKey(descriptor)){
            if(counter.get() < MAX_ENTRIES){
                if(!sparseCache.containsKey(descriptor)){
                    counter.incrementAndGet();
                    DataBuffer buffer = Shape.createSparseInformation(flags, sparseOffsets, hiddenDimensions, underlyingRank);
                    sparseCache.put(descriptor, buffer);
                    return buffer;
                }
            } else {
                return Shape.createSparseInformation(flags, sparseOffsets, hiddenDimensions, underlyingRank);
            }
        }
        return sparseCache.get(descriptor);
    }
    
    @Override
    public void purgeCache() {
        sparseCache = new ConcurrentHashMap<>();
    }

}

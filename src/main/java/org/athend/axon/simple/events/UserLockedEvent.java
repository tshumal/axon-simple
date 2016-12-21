
package org.athend.axon.simple.events;

import java.io.Serializable;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class UserLockedEvent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private UUID userId;


}

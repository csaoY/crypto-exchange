package com.itranswarp.crypto.match;

import java.util.ArrayList;
import java.util.List;

public class MatchResult {

	public List<MatchRecord> matchRecords;

	public void addMatchRecord(MatchRecord matchRecord) {
		if (matchRecords == null) {
			matchRecords = new ArrayList<>();
		}
		matchRecords.add(matchRecord);
	}
}

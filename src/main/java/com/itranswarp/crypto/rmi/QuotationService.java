package com.itranswarp.crypto.rmi;

import java.rmi.Remote;

import com.itranswarp.crypto.message.KLine;

public interface QuotationService extends Remote {

	KLine[] getKLine(KLine.Type type);

}

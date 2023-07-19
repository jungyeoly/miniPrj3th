package com.example.myapp.purchase.service;

import java.util.List;

import com.example.myapp.purchase.model.Purchase;
import com.example.myapp.purchase.model.PurchaseDetail;

public interface IPurchaseService {
	void insertAllPurchase(Purchase purchase, PurchaseDetail purchaseDetail);
	
	List<Purchase> selectAllPurchase(String userId);
	PurchaseDetail selectPurchase(int purchaseId, String userId);
}

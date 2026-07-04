package main

import (
	"encoding/json"
	"fmt"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
)

type SentinellaLedger struct {
	contractapi.Contract
}

type hashRecord struct {
	EntityType  string `json:"entityType"`
	EntityID    string `json:"entityId"`
	NodeID      string `json:"nodeId"`
	ContentHash string `json:"contentHash"`
	RecordID    string `json:"recordId"`
}

// RegisterHash ancla un hash de evento critico (append-only por entityType+entityId).
func (s *SentinellaLedger) RegisterHash(
	ctx contractapi.TransactionContextInterface,
	entityType string,
	entityId string,
	nodeId string,
	contentHash string,
	recordId string,
) (string, error) {
	key, err := ctx.GetStub().CreateCompositeKey("hash", []string{entityType, entityId})
	if err != nil {
		return "", err
	}
	existing, err := ctx.GetStub().GetState(key)
	if err != nil {
		return "", err
	}
	if existing != nil {
		return "", fmt.Errorf("hash ya registrado para %s:%s", entityType, entityId)
	}
	record := hashRecord{
		EntityType:  entityType,
		EntityID:    entityId,
		NodeID:      nodeId,
		ContentHash: contentHash,
		RecordID:    recordId,
	}
	payload, err := json.Marshal(record)
	if err != nil {
		return "", err
	}
	if err := ctx.GetStub().PutState(key, payload); err != nil {
		return "", err
	}
	return ctx.GetStub().GetTxID(), nil
}

// GetHash devuelve el hash almacenado para verificacion.
func (s *SentinellaLedger) GetHash(
	ctx contractapi.TransactionContextInterface,
	entityType string,
	entityId string,
) (string, error) {
	key, err := ctx.GetStub().CreateCompositeKey("hash", []string{entityType, entityId})
	if err != nil {
		return "", err
	}
	data, err := ctx.GetStub().GetState(key)
	if err != nil {
		return "", err
	}
	if data == nil {
		return "", fmt.Errorf("hash no encontrado para %s:%s", entityType, entityId)
	}
	var record hashRecord
	if err := json.Unmarshal(data, &record); err != nil {
		return "", err
	}
	return record.ContentHash, nil
}

func main() {
	chaincode, err := contractapi.NewChaincode(&SentinellaLedger{})
	if err != nil {
		panic(err)
	}
	if err := chaincode.Start(); err != nil {
		panic(err)
	}
}

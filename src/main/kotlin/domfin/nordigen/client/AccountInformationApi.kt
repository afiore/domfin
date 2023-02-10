package domfin.nordigen.client

import domfin.nordigen.Institution
import domfin.nordigen.Requisition
import domfin.nordigen.RequisitionRequest
import domfin.nordigen.RequisitionResponse


abstract class AccountInformationApi : GetTransactionsApi(), GetAllRequistions {
    abstract suspend fun getInstitutions(countryCode: String): List<Institution>
    abstract suspend fun createRequisition(requisitionRequest: RequisitionRequest): RequisitionResponse
    abstract suspend fun getRequisition(requisitionId: String): Requisition
}


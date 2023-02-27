package domfin.nordigen.client

import domfin.nordigen.Requisition

interface GetAllRequistions {
    suspend fun getAllRequisitions(): List<Requisition>
}

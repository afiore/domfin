package domfin.nordigen.client

import domfin.nordigen.Requisition

interface GetAllRequistions {
    abstract suspend fun getAllRequisitions(): List<Requisition>
}

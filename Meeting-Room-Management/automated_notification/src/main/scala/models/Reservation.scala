package models

case class Reservation(
                        reservationId: Int,
                        roomId: Int,
                        employeeName: String,
                        department: String,
                        purpose: String,
                        startTime: String,
                        endTime: String,
                        createdBy: Int
                      )

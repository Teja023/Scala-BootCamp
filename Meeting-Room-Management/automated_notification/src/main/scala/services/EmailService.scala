package services

import models.Reservation

class EmailService {
  def sendBookingConfirmation(reservation: Reservation): Unit = {
    println(s"Sending booking confirmation to ${reservation.employeeName} for reservation reservationId ${reservation.reservationId}")
  }

  def sendRoomPreparationNotification(reservation: Reservation): Unit = {
    println(s"Sending room preparation notification for reservation reservationId ${reservation.reservationId}")
  }

  def sendReminder(reservation: Reservation): Unit = {
    println(s"Sending reminder to ${reservation.employeeName} for reservation reservationId ${reservation.reservationId}")
  }

  def sendReleaseNotification(reservation: Reservation): Unit = {
    println(s"Room ${reservation.roomId} was not used for reservation reservationId ${reservation.reservationId}. Notification sent to admin staff.")
  }
}

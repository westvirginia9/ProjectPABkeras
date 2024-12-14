package com.example.projectpabkeras

import android.os.Parcel
import android.os.Parcelable

data class Expense(
    val date: String, // Tanggal pengeluaran
    val description: String, // Deskripsi pengeluaran
    val amount: Double // Jumlah pengeluaran
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(date)
        parcel.writeString(description)
        parcel.writeDouble(amount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense = Expense(parcel)
        override fun newArray(size: Int): Array<Expense?> = arrayOfNulls(size)
    }
}

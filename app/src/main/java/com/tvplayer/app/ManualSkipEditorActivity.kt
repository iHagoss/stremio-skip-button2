private fun showAddRangeDialog() {
    val dialogView = layoutInflater.inflate(R.layout.dialog_add_skip_range, null)
    val startInput = dialogView.findViewById<EditText>(R.id.startInput)
    val endInput = dialogView.findViewById<EditText>(R.id.endInput)
    val typeSpinner = dialogView.findViewById<Spinner>(R.id.typeSpinner)

    // Populate spinner with predefined skip types
    val types = resources.getStringArray(R.array.skip_type_options)
    val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    typeSpinner.adapter = spinnerAdapter

    AlertDialog.Builder(this)
        .setTitle("Add Skip Range")
        .setView(dialogView)
        .setPositiveButton("Save") { _, _ ->
            val start = startInput.text.toString().toDoubleOrNull() ?: 0.0
            val end = endInput.text.toString().toDoubleOrNull() ?: 0.0
            val type = typeSpinner.selectedItem.toString()

            val newRange = JSONObject().apply {
                put("start", start)
                put("end", end)
                put("type", type)
            }

            skipRanges.add("Start: ${start}s, End: ${end}s, Type: $type")
            saveRange(newRange)
            adapter.notifyDataSetChanged()
        }
        .setNegativeButton("Cancel", null)
        .show()
}

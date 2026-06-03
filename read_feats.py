import openpyxl
wb = openpyxl.load_workbook('Copy of Royal Kingdom Sheet for Pathfinder 2E - CURRENT.xlsx', data_only=True)
ws = wb['Tables']

print("=== Kingdom Feats (rows 76-120) ===")
for i in range(76, 120):
    row = list(ws.iter_rows(min_row=i, max_row=i, values_only=True))[0]
    # Only print the feat-relevant columns (cols 0-6)
    feat_cols = row[:7]
    if any(v is not None for v in feat_cols):
        print(f"Row {i}: {feat_cols}")

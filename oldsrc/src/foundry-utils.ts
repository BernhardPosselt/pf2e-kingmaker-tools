export async function openJournal(uuid: string): Promise<void> {
    const journal = await fromUuid(uuid) as JournalEntry | JournalEntryPage | null;
    if (journal instanceof JournalEntryPage) {
        journal?.parent?.sheet?.render(true, {pageId: journal.id});
    } else {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (journal as any).sheet.render(true);
    }
}
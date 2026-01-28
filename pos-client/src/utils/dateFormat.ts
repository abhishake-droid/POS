/**
 * Formats a date string or Date object into a human-readable format
 * Example: "27th Jan 2025"
 */
export function formatDateText(date: string | Date | null | undefined): string {
    if (!date) return '-';

    const dateObj = typeof date === 'string' ? new Date(date) : date;

    // Check if date is valid
    if (isNaN(dateObj.getTime())) return '-';

    const day = dateObj.getDate();
    const month = dateObj.toLocaleString('en-US', { month: 'short' });
    const year = dateObj.getFullYear();

    // Get ordinal suffix (st, nd, rd, th)
    const suffix = getOrdinalSuffix(day);

    return `${day}${suffix} ${month} ${year}`;
}

/**
 * Returns the ordinal suffix for a given day number
 */
function getOrdinalSuffix(day: number): string {
    if (day > 3 && day < 21) return 'th'; // 11th, 12th, 13th, etc.

    switch (day % 10) {
        case 1:
            return 'st';
        case 2:
            return 'nd';
        case 3:
            return 'rd';
        default:
            return 'th';
    }
}

/**
 * Formats a date with time in human-readable format
 * Example: "27th Jan 2025, 11:30 PM"
 */
export function formatDateTimeText(date: string | Date | null | undefined): string {
    if (!date) return '-';

    const dateObj = typeof date === 'string' ? new Date(date) : date;

    // Check if date is valid
    if (isNaN(dateObj.getTime())) return '-';

    const datePart = formatDateText(dateObj);
    const time = dateObj.toLocaleString('en-US', {
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });

    return `${datePart}, ${time}`;
}

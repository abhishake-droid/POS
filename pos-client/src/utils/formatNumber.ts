/**
 * Formats a number in Indian numbering system with commas
 * Examples:
 * 1000 → 1,000
 * 10000 → 10,000
 * 100000 → 1,00,000
 * 1000000 → 10,00,000
 */
export function formatIndianNumber(num: number | string): string {
    if (num === null || num === undefined) return '0';

    const numStr = typeof num === 'string' ? num : num.toString();
    const parts = numStr.split('.');
    const integerPart = parts[0];
    const decimalPart = parts[1];

    // Indian numbering: first 3 digits from right, then groups of 2
    let formatted = '';
    let count = 0;

    for (let i = integerPart.length - 1; i >= 0; i--) {
        if (count === 3 || (count > 3 && (count - 3) % 2 === 0)) {
            formatted = ',' + formatted;
        }
        formatted = integerPart[i] + formatted;
        count++;
    }

    return decimalPart ? `${formatted}.${decimalPart}` : formatted;
}

/**
 * Formats currency in Indian Rupees
 * Example: 100000 → ₹1,00,000
 */
export function formatINR(num: number | string): string {
    return `₹${formatIndianNumber(num)}`;
}
